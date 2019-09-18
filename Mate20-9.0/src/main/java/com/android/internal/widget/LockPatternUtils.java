package com.android.internal.widget;

import android.app.admin.DevicePolicyManager;
import android.app.admin.PasswordMetrics;
import android.app.trust.IStrongAuthTracker;
import android.app.trust.TrustManager;
import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.UserInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.IStorageManager;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Protocol;
import com.android.internal.widget.ICheckCredentialProgressCallback;
import com.android.internal.widget.ILockSettings;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.server.LocalServices;
import com.google.android.collect.Lists;
import huawei.cust.HwCustUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
import java.util.Objects;
import java.util.StringJoiner;
import libcore.util.HexEncoding;

public class LockPatternUtils {
    @Deprecated
    public static final String BIOMETRIC_WEAK_EVER_CHOSEN_KEY = "lockscreen.biometricweakeverchosen";
    public static final int CREDENTIAL_TYPE_NONE = -1;
    public static final int CREDENTIAL_TYPE_PASSWORD = 2;
    public static final int CREDENTIAL_TYPE_PATTERN = 1;
    private static final boolean DEBUG = false;
    public static final String DISABLE_LOCKSCREEN_KEY = "lockscreen.disabled";
    private static final String ENABLED_TRUST_AGENTS = "lockscreen.enabledtrustagents";
    public static final int FAILED_ATTEMPTS_BEFORE_RESET = 20;
    public static final int FAILED_ATTEMPTS_BEFORE_WIPE_GRACE = 5;
    public static final long FAILED_ATTEMPT_COUNTDOWN_INTERVAL_MS = 1000;
    public static final long FAILED_ATTEMPT_TIMEOUT_MS = 30000;
    private static final boolean FRP_CREDENTIAL_ENABLED = true;
    private static final String HISTORY_DELIMITER = ",";
    private static final String IS_TRUST_USUALLY_MANAGED = "lockscreen.istrustusuallymanaged";
    private static final String KEY_PREFIX = "error_type_";
    public static final String LEGACY_LOCK_PATTERN_ENABLED = "legacy_lock_pattern_enabled";
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
    public static final int MIN_LOCK_PASSWORD_SIZE = 4;
    public static final int MIN_LOCK_PATTERN_SIZE = 4;
    public static final int MIN_PATTERN_REGISTER_FAIL = 4;
    private static final long ONE_DAY = 86400000;
    public static final String PASSWORD_HISTORY_KEY = "lockscreen.passwordhistory";
    @Deprecated
    public static final String PASSWORD_TYPE_ALTERNATE_KEY = "lockscreen.password_type_alternate";
    public static final String PASSWORD_TYPE_KEY = "lockscreen.password_type";
    public static final String PATTERN_EVER_CHOSEN_KEY = "lockscreen.patterneverchosen";
    public static final String PROFILE_KEY_NAME_DECRYPT = "profile_key_name_decrypt_";
    public static final String PROFILE_KEY_NAME_ENCRYPT = "profile_key_name_encrypt_";
    private static final String SPF_XML_NAME = "check_password_exception";
    public static final String SYNTHETIC_PASSWORD_ENABLED_KEY = "enable-sp";
    public static final String SYNTHETIC_PASSWORD_HANDLE_KEY = "sp-handle";
    public static final String SYNTHETIC_PASSWORD_KEY_PREFIX = "synthetic_password_";
    private static final String TAG = "LockPatternUtils";
    public static final int USER_FRP = -9999;
    private static HwCustLockPatternUtils mCustLockPatternUtils = ((HwCustLockPatternUtils) HwCustUtils.createObj(HwCustLockPatternUtils.class, new Object[0]));
    private static HwFrameworkMonitor mMonitor = HwFrameworkFactory.getHwFrameworkMonitor();
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private DevicePolicyManager mDevicePolicyManager;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private ILockSettings mLockSettingsService;
    protected final SparseLongArray mLockoutDeadlines = new SparseLongArray();
    private UserManager mUserManager;

    public interface CheckCredentialProgressCallback {
        void onEarlyMatched();
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
        private static final int ALLOWING_FINGERPRINT = 4;
        public static final int SOME_AUTH_REQUIRED_AFTER_USER_REQUEST = 4;
        public static final int STRONG_AUTH_NOT_REQUIRED = 0;
        public static final int STRONG_AUTH_REQUIRED_AFTER_BOOT = 1;
        public static final int STRONG_AUTH_REQUIRED_AFTER_DPM_LOCK_NOW = 2;
        public static final int STRONG_AUTH_REQUIRED_AFTER_LOCKOUT = 8;
        public static final int STRONG_AUTH_REQUIRED_AFTER_TIMEOUT = 16;
        public static final int STRONG_AUTH_REQUIRED_AFTER_USER_LOCKDOWN = 32;
        private final int mDefaultStrongAuthFlags;
        /* access modifiers changed from: private */
        public final H mHandler;
        private final SparseIntArray mStrongAuthRequiredForUser;
        protected final IStrongAuthTracker.Stub mStub;

        private class H extends Handler {
            static final int MSG_ON_STRONG_AUTH_REQUIRED_CHANGED = 1;

            public H(Looper looper) {
                super(looper);
            }

            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    StrongAuthTracker.this.handleStrongAuthRequiredChanged(msg.arg1, msg.arg2);
                }
            }
        }

        @Retention(RetentionPolicy.SOURCE)
        public @interface StrongAuthFlags {
        }

        public StrongAuthTracker(Context context) {
            this(context, Looper.myLooper());
        }

        public StrongAuthTracker(Context context, Looper looper) {
            this.mStrongAuthRequiredForUser = new SparseIntArray();
            this.mStub = new IStrongAuthTracker.Stub() {
                public void onStrongAuthRequiredChanged(int strongAuthFlags, int userId) {
                    StrongAuthTracker.this.mHandler.obtainMessage(1, strongAuthFlags, userId).sendToTarget();
                }
            };
            this.mHandler = new H(looper);
            this.mDefaultStrongAuthFlags = getDefaultFlags(context);
        }

        public static int getDefaultFlags(Context context) {
            return context.getResources().getBoolean(17957032);
        }

        public int getStrongAuthForUser(int userId) {
            return this.mStrongAuthRequiredForUser.get(userId, this.mDefaultStrongAuthFlags);
        }

        public boolean isTrustAllowedForUser(int userId) {
            return getStrongAuthForUser(userId) == 0;
        }

        public boolean isFingerprintAllowedForUser(int userId) {
            return (getStrongAuthForUser(userId) & -5) == 0;
        }

        public void onStrongAuthRequiredChanged(int userId) {
        }

        /* access modifiers changed from: protected */
        public void handleStrongAuthRequiredChanged(int strongAuthFlags, int userId) {
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
                return getLockSettings().getBoolean(IS_TRUST_USUALLY_MANAGED, false, userId);
            } catch (RemoteException e) {
                return false;
            }
        } else {
            throw new IllegalStateException("May only be called by TrustManagerService. Use TrustManager.isTrustUsuallyManaged()");
        }
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
        Looper looper = Looper.myLooper();
        this.mHandler = looper != null ? new Handler(looper) : null;
        if (mCustLockPatternUtils != null && mCustLockPatternUtils.isForbiddenSimplePwdFeatureEnable()) {
            mCustLockPatternUtils.initHwCustLockPatternUtils(getDevicePolicyManager(), this.mContext);
        }
    }

    @VisibleForTesting
    public ILockSettings getLockSettings() {
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
        if (userId != -9999 || !frpCredentialEnabled(this.mContext)) {
            getDevicePolicyManager().reportFailedPasswordAttempt(userId);
            getTrustManager().reportUnlockAttempt(false, userId);
        }
    }

    public void reportSuccessfulPasswordAttempt(int userId) {
        if (userId != -9999 || !frpCredentialEnabled(this.mContext)) {
            getDevicePolicyManager().reportSuccessfulPasswordAttempt(userId);
            getTrustManager().reportUnlockAttempt(true, userId);
        }
    }

    public void reportPasswordLockout(int timeoutMs, int userId) {
        if (userId != -9999 || !frpCredentialEnabled(this.mContext)) {
            getTrustManager().reportUnlockLockout(timeoutMs, userId);
        }
    }

    public int getCurrentFailedPasswordAttempts(int userId) {
        if (userId != -9999 || !frpCredentialEnabled(this.mContext)) {
            return getDevicePolicyManager().getCurrentFailedPasswordAttempts(userId);
        }
        return 0;
    }

    public int getMaximumFailedPasswordsForWipe(int userId) {
        if (userId != -9999 || !frpCredentialEnabled(this.mContext)) {
            return getDevicePolicyManager().getMaximumFailedPasswordsForWipe(null, userId);
        }
        return 0;
    }

    private byte[] verifyCredential(String credential, int type, long challenge, int userId) throws RequestThrottledException {
        try {
            Log.i(TAG, "verifyCredential userId:" + userId);
            VerifyCredentialResponse response = getLockSettings().verifyCredential(credential, type, challenge, userId);
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

    private boolean checkCredential(String credential, int type, int userId, CheckCredentialProgressCallback progressCallback) throws RequestThrottledException {
        try {
            if (getLockSettings() == null) {
                monitorCheckPassword(1006, null);
                return false;
            }
            VerifyCredentialResponse response = getLockSettings().checkCredential(credential, type, userId, wrapCallback(progressCallback));
            if (response.getResponseCode() == 0) {
                return true;
            }
            if (response.getResponseCode() != 1) {
                return false;
            }
            RequestThrottledException e = new RequestThrottledException(response.getTimeout());
            e.fillInStackTrace();
            monitorCheckPassword(1003, e);
            throw new RequestThrottledException(response.getTimeout());
        } catch (RemoteException re) {
            monitorCheckPassword(1004, re);
            return false;
        } catch (SecurityException se) {
            monitorCheckPassword(1005, se);
            return false;
        }
    }

    public byte[] verifyPattern(List<LockPatternView.Cell> pattern, long challenge, int userId) throws RequestThrottledException {
        throwIfCalledOnMainThread();
        return verifyCredential(patternToString(pattern), 1, challenge, userId);
    }

    public boolean checkPattern(List<LockPatternView.Cell> pattern, int userId) throws RequestThrottledException {
        return checkPattern(pattern, userId, null);
    }

    public boolean checkPattern(List<LockPatternView.Cell> pattern, int userId, CheckCredentialProgressCallback progressCallback) throws RequestThrottledException {
        throwIfCalledOnMainThread();
        return checkCredential(patternToString(pattern), 1, userId, progressCallback);
    }

    public byte[] verifyPassword(String password, long challenge, int userId) throws RequestThrottledException {
        throwIfCalledOnMainThread();
        return verifyCredential(password, 2, challenge, userId);
    }

    public byte[] verifyTiedProfileChallenge(String password, boolean isPattern, long challenge, int userId) throws RequestThrottledException {
        throwIfCalledOnMainThread();
        try {
            VerifyCredentialResponse response = getLockSettings().verifyTiedProfileChallenge(password, isPattern ? 1 : 2, challenge, userId);
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
        return checkPassword(password, userId, null);
    }

    public boolean checkPassword(String password, int userId, CheckCredentialProgressCallback progressCallback) throws RequestThrottledException {
        throwIfCalledOnMainThread();
        if (password != null && !password.equals("")) {
            return checkCredential(password, 2, userId, progressCallback);
        }
        monitorCheckPassword(1002, null);
        return false;
    }

    public boolean checkVoldPassword(int userId) {
        try {
            return getLockSettings().checkVoldPassword(userId);
        } catch (RemoteException e) {
            return false;
        }
    }

    public byte[] getPasswordHistoryHashFactor(String currentPassword, int userId) {
        try {
            return getLockSettings().getHashFactor(currentPassword, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "failed to get hash factor", e);
            return null;
        }
    }

    public boolean checkPasswordHistory(String passwordToCheck, byte[] hashFactor, int userId) {
        if (TextUtils.isEmpty(passwordToCheck)) {
            Log.e(TAG, "checkPasswordHistory: empty password");
            return false;
        }
        String passwordHistory = getString(PASSWORD_HISTORY_KEY, userId);
        if (TextUtils.isEmpty(passwordHistory)) {
            return false;
        }
        int passwordHistoryLength = getRequestedPasswordHistoryLength(userId);
        if (passwordHistoryLength == 0) {
            return false;
        }
        String legacyHash = legacyPasswordToHash(passwordToCheck, userId);
        String passwordHash = passwordToHistoryHash(passwordToCheck, hashFactor, userId);
        String[] history = passwordHistory.split(HISTORY_DELIMITER);
        for (int i = 0; i < Math.min(passwordHistoryLength, history.length); i++) {
            if (history[i].equals(legacyHash) || history[i].equals(passwordHash)) {
                return true;
            }
        }
        return false;
    }

    private boolean savedPatternExists(int userId) {
        try {
            return getLockSettings().havePattern(userId);
        } catch (RemoteException e) {
            return false;
        }
    }

    private boolean savedPasswordExists(int userId) {
        try {
            return getLockSettings().havePassword(userId);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isPatternEverChosen(int userId) {
        return getBoolean(PATTERN_EVER_CHOSEN_KEY, false, userId);
    }

    public void reportPatternWasChosen(int userId) {
        setBoolean(PATTERN_EVER_CHOSEN_KEY, true, userId);
    }

    public int getActivePasswordQuality(int userId) {
        int quality = getKeyguardStoredPasswordQuality(userId);
        if (!isLockPasswordEnabled(quality, userId) && !isLockPatternEnabled(quality, userId)) {
            return 0;
        }
        return quality;
    }

    public void resetKeyStore(int userId) {
        try {
            getLockSettings().resetKeyStore(userId);
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't reset keystore " + e);
        }
    }

    public void clearLock(String savedCredential, int userHandle) {
        int currentQuality = getKeyguardStoredPasswordQuality(userHandle);
        setKeyguardStoredPasswordQuality(0, userHandle);
        try {
            getLockSettings().setLockCredential(null, -1, savedCredential, 0, userHandle);
            Log.i(TAG, "clearLock success by uid = " + Binder.getCallingUid());
            if (userHandle == 0) {
                updateEncryptionPassword(1, null);
                setCredentialRequiredToDecrypt(false);
            }
            onAfterChangingPassword(userHandle);
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear lock", e);
            setKeyguardStoredPasswordQuality(currentQuality, userHandle);
        }
    }

    public void setLockScreenDisabled(boolean disable, int userId) {
        setBoolean(DISABLE_LOCKSCREEN_KEY, disable, userId);
    }

    public boolean isLockScreenDisabled(int userId) {
        boolean z = false;
        if (isSecure(userId)) {
            return false;
        }
        boolean disabledByDefault = this.mContext.getResources().getBoolean(17956928);
        boolean isSystemUser = UserManager.isSplitSystemUser() && userId == 0;
        UserInfo userInfo = getUserManager().getUserInfo(userId);
        boolean isDemoUser = UserManager.isDeviceInDemoMode(this.mContext) && userInfo != null && userInfo.isDemo();
        if (getBoolean(DISABLE_LOCKSCREEN_KEY, false, userId) || ((disabledByDefault && !isSystemUser) || isDemoUser)) {
            z = true;
        }
        return z;
    }

    public void saveLockPattern(List<LockPatternView.Cell> pattern, int userId) {
        saveLockPattern(pattern, null, userId);
    }

    public void saveLockPattern(List<LockPatternView.Cell> pattern, String savedPattern, int userId) {
        if (pattern == null || pattern.size() < 4) {
            throw new IllegalArgumentException("pattern must not be null and at least 4 dots long.");
        }
        writeSettingsData();
        String stringPattern = patternToString(pattern);
        int currentQuality = getKeyguardStoredPasswordQuality(userId);
        setKeyguardStoredPasswordQuality(Protocol.BASE_SYSTEM_RESERVED, userId);
        try {
            getLockSettings().setLockCredential(stringPattern, 1, savedPattern, Protocol.BASE_SYSTEM_RESERVED, userId);
            if (userId == 0 && isDeviceEncryptionEnabled()) {
                if (!shouldEncryptWithCredentials(true)) {
                    clearEncryptionPassword();
                } else {
                    updateEncryptionPassword(3, stringPattern);
                }
            }
            reportPatternWasChosen(userId);
            onAfterChangingPassword(userId);
            Log.i(TAG, "saveLockPattern success by uid = " + Binder.getCallingUid());
        } catch (Exception e) {
            Log.e(TAG, "Couldn't save lock pattern", e);
            setKeyguardStoredPasswordQuality(currentQuality, userId);
        }
    }

    private void updateCryptoUserInfo(int userId) {
        if (userId == 0) {
            String ownerInfo = isOwnerInfoEnabled(userId) ? getOwnerInfo(userId) : "";
            IBinder service = ServiceManager.getService("mount");
            if (service == null) {
                Log.e(TAG, "Could not find the mount service to update the user info");
                return;
            }
            IStorageManager storageManager = IStorageManager.Stub.asInterface(service);
            try {
                Log.d(TAG, "Setting owner info");
                storageManager.setField("OwnerInfo", ownerInfo);
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
        return getBoolean(LOCK_SCREEN_OWNER_INFO_ENABLED, false, userId);
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
        return getDeviceOwnerInfo() != null;
    }

    /* access modifiers changed from: protected */
    public void updateEncryptionPassword(final int type, final String password) {
        if (isDeviceEncryptionEnabled()) {
            final IBinder service = ServiceManager.getService("mount");
            if (service == null) {
                Log.e(TAG, "Could not find the mount service to update the encryption password");
            } else {
                new AsyncTask<Void, Void, Void>() {
                    /* access modifiers changed from: protected */
                    public Void doInBackground(Void... dummy) {
                        try {
                            IStorageManager.Stub.asInterface(service).changeEncryptionPassword(type, password);
                        } catch (RemoteException e) {
                            Log.e(LockPatternUtils.TAG, "Error changing encryption password", e);
                        }
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
            }
        }
    }

    public void saveLockPassword(String password, String savedPassword, int requestedQuality, int userHandle) {
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("password must not be null and at least of length 4");
        }
        int computedQuality = PasswordMetrics.computeForPassword(password).quality;
        writeSettingsData();
        int currentQuality = getKeyguardStoredPasswordQuality(userHandle);
        setKeyguardStoredPasswordQuality(computePasswordQuality(2, password, requestedQuality), userHandle);
        try {
            getLockSettings().setLockCredential(password, 2, savedPassword, requestedQuality, userHandle);
            updateEncryptionPasswordIfNeeded(password, PasswordMetrics.computeForPassword(password).quality, userHandle);
            updatePasswordHistory(password, userHandle);
            if (mCustLockPatternUtils != null && mCustLockPatternUtils.isForbiddenSimplePwdFeatureEnable()) {
                if (computedQuality != 0) {
                    mCustLockPatternUtils.saveCurrentPwdStatus(mCustLockPatternUtils.currentpwdSimpleCheck(password));
                } else {
                    mCustLockPatternUtils.saveCurrentPwdStatus(true);
                }
            }
            Log.i(TAG, "saveLockPattern success by uid = " + Binder.getCallingUid());
            onAfterChangingPassword(userHandle);
        } catch (Exception e) {
            Log.e(TAG, "Unable to save lock password", e);
            setKeyguardStoredPasswordQuality(currentQuality, userHandle);
        }
    }

    private void writeSettingsData() {
        try {
            String subjectDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date(System.currentTimeMillis()));
            if (TextUtils.isEmpty(Settings.System.getString(this.mContext.getContentResolver(), "lock_settings_time"))) {
                Settings.System.putString(this.mContext.getContentResolver(), "first_lock_time", subjectDate);
            }
            Settings.System.putString(this.mContext.getContentResolver(), "lock_settings_time", subjectDate);
        } catch (Exception e) {
            Log.e(TAG, "failed to write settings", e);
        }
    }

    private void updateEncryptionPasswordIfNeeded(String password, int quality, int userHandle) {
        if (userHandle == 0 && isDeviceEncryptionEnabled()) {
            boolean numericComplex = true;
            if (!shouldEncryptWithCredentials(true)) {
                clearEncryptionPassword();
                return;
            }
            int type = 0;
            boolean numeric = quality == 131072;
            if (quality != 196608) {
                numericComplex = false;
            }
            if (numeric || numericComplex) {
                type = 2;
            }
            updateEncryptionPassword(type, password);
        }
    }

    private void updatePasswordHistory(String password, int userHandle) {
        String passwordHistory;
        if (TextUtils.isEmpty(password)) {
            Log.e(TAG, "checkPasswordHistory: empty password");
            return;
        }
        String passwordHistory2 = getString(PASSWORD_HISTORY_KEY, userHandle);
        if (passwordHistory2 == null) {
            passwordHistory2 = "";
        }
        int passwordHistoryLength = getRequestedPasswordHistoryLength(userHandle);
        if (passwordHistoryLength == 0) {
            passwordHistory = "";
        } else {
            String hash = passwordToHistoryHash(password, getPasswordHistoryHashFactor(password, userHandle), userHandle);
            if (hash == null) {
                Log.e(TAG, "Compute new style password hash failed, fallback to legacy style");
                hash = legacyPasswordToHash(password, userHandle);
            }
            if (TextUtils.isEmpty(passwordHistory2)) {
                passwordHistory = hash;
            } else {
                String[] history = passwordHistory2.split(HISTORY_DELIMITER);
                StringJoiner joiner = new StringJoiner(HISTORY_DELIMITER);
                joiner.add(hash);
                int i = 0;
                while (i < passwordHistoryLength - 1 && i < history.length) {
                    joiner.add(history[i]);
                    i++;
                }
                passwordHistory = joiner.toString();
            }
        }
        setString(PASSWORD_HISTORY_KEY, passwordHistory, userHandle);
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

    private void setKeyguardStoredPasswordQuality(int quality, int userHandle) {
        setLong(PASSWORD_TYPE_KEY, (long) quality, userHandle);
    }

    private int computePasswordQuality(int type, String credential, int requestedQuality) {
        if (type == 2) {
            return Math.max(requestedQuality, PasswordMetrics.computeForPassword(credential).quality);
        }
        if (type == 1) {
            return Protocol.BASE_SYSTEM_RESERVED;
        }
        return 0;
    }

    public void setSeparateProfileChallengeEnabled(int userHandle, boolean enabled, String managedUserPassword) {
        if (isManagedProfile(userHandle)) {
            try {
                getLockSettings().setSeparateProfileChallengeEnabled(userHandle, enabled, managedUserPassword);
                onAfterChangingPassword(userHandle);
            } catch (RemoteException e) {
                Log.e(TAG, "Couldn't update work profile challenge enabled");
            }
        }
    }

    public boolean isSeparateProfileChallengeEnabled(int userHandle) {
        return isManagedProfile(userHandle) && hasSeparateChallenge(userHandle);
    }

    public boolean isManagedProfileWithUnifiedChallenge(int userHandle) {
        return isManagedProfile(userHandle) && !hasSeparateChallenge(userHandle);
    }

    public boolean isSeparateProfileChallengeAllowed(int userHandle) {
        return isManagedProfile(userHandle) && getDevicePolicyManager().isSeparateProfileChallengeAllowed(userHandle);
    }

    public boolean isSeparateProfileChallengeAllowedToUnify(int userHandle) {
        return getDevicePolicyManager().isProfileActivePasswordSufficientForParent(userHandle) && !getUserManager().hasUserRestriction("no_unified_password", UserHandle.of(userHandle));
    }

    private boolean hasSeparateChallenge(int userHandle) {
        try {
            return getLockSettings().getSeparateProfileChallengeEnabled(userHandle);
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't get separate profile challenge enabled");
            return false;
        }
    }

    private boolean isManagedProfile(int userHandle) {
        UserInfo info = getUserManager().getUserInfo(userHandle);
        return info != null && info.isManagedProfile();
    }

    public static List<LockPatternView.Cell> stringToPattern(String string) {
        if (string == null) {
            return null;
        }
        List<LockPatternView.Cell> result = Lists.newArrayList();
        byte[] bytes = string.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            byte b = (byte) (bytes[i] - 49);
            result.add(LockPatternView.Cell.of(b / 3, b % 3));
        }
        return result;
    }

    public static String patternToString(List<LockPatternView.Cell> pattern) {
        if (pattern == null) {
            return "";
        }
        int patternSize = pattern.size();
        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            LockPatternView.Cell cell = pattern.get(i);
            res[i] = (byte) ((cell.getRow() * 3) + cell.getColumn() + 49);
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

    public static byte[] patternToHash(List<LockPatternView.Cell> pattern) {
        if (pattern == null) {
            return null;
        }
        int patternSize = pattern.size();
        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            LockPatternView.Cell cell = pattern.get(i);
            res[i] = (byte) ((cell.getRow() * 3) + cell.getColumn());
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

    public String legacyPasswordToHash(String password, int userId) {
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
            return new String(HexEncoding.encode(combined));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("Missing digest algorithm: ", e);
        }
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

    private String passwordToHistoryHash(String passwordToHash, byte[] hashFactor, int userId) {
        if (TextUtils.isEmpty(passwordToHash) || hashFactor == null) {
            return null;
        }
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(hashFactor);
            sha256.update((passwordToHash + getSalt(userId)).getBytes());
            return new String(HexEncoding.encode(sha256.digest()));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("Missing digest algorithm: ", e);
        }
    }

    public boolean isSecure(int userId) {
        int mode = getKeyguardStoredPasswordQuality(userId);
        return isLockPatternEnabled(mode, userId) || isLockPasswordEnabled(mode, userId);
    }

    public boolean isLockPasswordEnabled(int userId) {
        return isLockPasswordEnabled(getKeyguardStoredPasswordQuality(userId), userId);
    }

    private boolean isLockPasswordEnabled(int mode, int userId) {
        if (!(mode == 262144 || mode == 131072 || mode == 196608 || mode == 327680 || mode == 393216 || mode == 524288) || !savedPasswordExists(userId)) {
            return false;
        }
        return true;
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
        return mode == 65536 && savedPatternExists(userId);
    }

    public boolean isVisiblePatternEnabled(int userId) {
        return getBoolean("lock_pattern_visible_pattern", false, userId);
    }

    public void setVisiblePatternEnabled(boolean enabled, int userId) {
        setBoolean("lock_pattern_visible_pattern", enabled, userId);
        if (userId == 0) {
            IBinder service = ServiceManager.getService("mount");
            if (service == null) {
                Log.e(TAG, "Could not find the mount service to update the user info");
                return;
            }
            try {
                IStorageManager.Stub.asInterface(service).setField("PatternVisible", enabled ? "1" : "0");
            } catch (RemoteException e) {
                Log.e(TAG, "Error changing pattern visible state", e);
            }
        }
    }

    public boolean isVisiblePatternEverChosen(int userId) {
        return getString("lock_pattern_visible_pattern", userId) != null;
    }

    public void setVisiblePasswordEnabled(boolean enabled, int userId) {
        if (userId == 0) {
            IBinder service = ServiceManager.getService("mount");
            if (service == null) {
                Log.e(TAG, "Could not find the mount service to update the user info");
                return;
            }
            try {
                IStorageManager.Stub.asInterface(service).setField("PasswordVisible", enabled ? "1" : "0");
            } catch (RemoteException e) {
                Log.e(TAG, "Error changing password visible state", e);
            }
        }
    }

    public boolean isTactileFeedbackEnabled() {
        return Settings.System.getIntForUser(this.mContentResolver, "haptic_feedback_enabled", 1, -2) != 0;
    }

    public long setLockoutAttemptDeadline(int userId, int timeoutMs) {
        long deadline = SystemClock.elapsedRealtime() + (mCustLockPatternUtils.needLimit() ? FAILED_ATTEMPT_TIMEOUT_MS : (long) timeoutMs);
        if (userId == -9999) {
            return deadline;
        }
        this.mLockoutDeadlines.put(userId, deadline);
        return deadline;
    }

    public long getLockoutAttemptDeadline(int userId) {
        long deadline = this.mLockoutDeadlines.get(userId, 0);
        if (deadline >= SystemClock.elapsedRealtime() || deadline == 0) {
            return deadline;
        }
        this.mLockoutDeadlines.put(userId, 0);
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

    /* access modifiers changed from: protected */
    public void setLong(String secureSettingKey, long value, int userHandle) {
        try {
            getLockSettings().setLong(secureSettingKey, value, userHandle);
        } catch (RemoteException re) {
            Log.e(TAG, "Couldn't write long " + secureSettingKey + re);
        }
    }

    /* access modifiers changed from: protected */
    public String getString(String secureSettingKey, int userHandle) {
        try {
            return getLockSettings().getString(secureSettingKey, null, userHandle);
        } catch (RemoteException e) {
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void setString(String secureSettingKey, String value, int userHandle) {
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
        if (mCustLockPatternUtils == null || mCustLockPatternUtils.getPowerBtnInstantlyLockDefault()) {
            return getBoolean(LOCKSCREEN_POWER_BUTTON_INSTANTLY_LOCKS, true, userId);
        }
        return getBoolean(LOCKSCREEN_POWER_BUTTON_INSTANTLY_LOCKS, false, userId);
    }

    public boolean isPowerButtonInstantlyLocksEverChosen(int userId) {
        return getString(LOCKSCREEN_POWER_BUTTON_INSTANTLY_LOCKS, userId) != null;
    }

    public void setEnabledTrustAgents(Collection<ComponentName> activeTrustAgents, int userId) {
        StringBuilder sb = new StringBuilder();
        for (ComponentName cn : activeTrustAgents) {
            if (sb.length() > 0) {
                sb.append(',');
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
        String[] split = serialized.split(HISTORY_DELIMITER);
        ArrayList<ComponentName> activeTrustAgents = new ArrayList<>(split.length);
        for (String s : split) {
            if (!TextUtils.isEmpty(s)) {
                activeTrustAgents.add(ComponentName.unflattenFromString(s));
            }
        }
        return activeTrustAgents;
    }

    public void requireCredentialEntry(int userId) {
        requireStrongAuth(4, userId);
    }

    public void requireStrongAuth(int strongAuthReason, int userId) {
        try {
            getLockSettings().requireStrongAuth(strongAuthReason, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "Error while requesting strong auth: " + e);
        }
    }

    /* access modifiers changed from: protected */
    public void onAfterChangingPassword(int userHandle) {
        getTrustManager().reportEnabledTrustAgentsChanged(userHandle);
    }

    public boolean isCredentialRequiredToDecrypt(boolean defaultValue) {
        int value = Settings.Global.getInt(this.mContentResolver, "require_password_to_decrypt", -1);
        if (value == -1) {
            return defaultValue;
        }
        return value != 0;
    }

    public void setCredentialRequiredToDecrypt(boolean required) {
        if (!getUserManager().isSystemUser() && !getUserManager().isPrimaryUser()) {
            throw new IllegalStateException("Only the system or primary user may call setCredentialRequiredForDecrypt()");
        } else if (isDeviceEncryptionEnabled()) {
            Settings.Global.putInt(this.mContext.getContentResolver(), "require_password_to_decrypt", required);
        }
    }

    private boolean isDoNotAskCredentialsOnBootSet() {
        return getDevicePolicyManager().getDoNotAskCredentialsOnBoot();
    }

    /* access modifiers changed from: protected */
    public boolean shouldEncryptWithCredentials(boolean defaultValue) {
        return isCredentialRequiredToDecrypt(defaultValue) && !isDoNotAskCredentialsOnBootSet();
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
        return getStrongAuthForUser(userId) == 0;
    }

    public boolean isFingerprintAllowedForUser(int userId) {
        return (getStrongAuthForUser(userId) & -5) == 0;
    }

    public boolean isUserInLockdown(int userId) {
        return getStrongAuthForUser(userId) == 32;
    }

    private ICheckCredentialProgressCallback wrapCallback(final CheckCredentialProgressCallback callback) {
        if (callback == null) {
            return null;
        }
        if (this.mHandler != null) {
            return new ICheckCredentialProgressCallback.Stub() {
                public void onCredentialVerified() throws RemoteException {
                    Handler access$000 = LockPatternUtils.this.mHandler;
                    CheckCredentialProgressCallback checkCredentialProgressCallback = callback;
                    Objects.requireNonNull(checkCredentialProgressCallback);
                    access$000.post(new Runnable() {
                        public final void run() {
                            LockPatternUtils.CheckCredentialProgressCallback.this.onEarlyMatched();
                        }
                    });
                }
            };
        }
        throw new IllegalStateException("Must construct LockPatternUtils on a looper thread to use progress callbacks.");
    }

    private LockSettingsInternal getLockSettingsInternal() {
        LockSettingsInternal service = (LockSettingsInternal) LocalServices.getService(LockSettingsInternal.class);
        if (service != null) {
            return service;
        }
        throw new SecurityException("Only available to system server itself");
    }

    public long addEscrowToken(byte[] token, int userId) {
        return getLockSettingsInternal().addEscrowToken(token, userId);
    }

    public boolean removeEscrowToken(long handle, int userId) {
        return getLockSettingsInternal().removeEscrowToken(handle, userId);
    }

    public boolean isEscrowTokenActive(long handle, int userId) {
        return getLockSettingsInternal().isEscrowTokenActive(handle, userId);
    }

    public boolean setLockCredentialWithToken(String credential, int type, int requestedQuality, long tokenHandle, byte[] token, int userId) {
        boolean z;
        String str = credential;
        int i = type;
        int quality = userId;
        LockSettingsInternal localService = getLockSettingsInternal();
        if (i == -1) {
            int i2 = requestedQuality;
            if (!TextUtils.isEmpty(credential)) {
                throw new IllegalArgumentException("password must be emtpy for NONE type");
            } else if (!localService.setLockCredentialWithToken(null, -1, tokenHandle, token, 0, quality)) {
                return false;
            } else {
                setKeyguardStoredPasswordQuality(0, quality);
                if (quality == 0) {
                    z = true;
                    updateEncryptionPassword(1, null);
                    setCredentialRequiredToDecrypt(false);
                    onAfterChangingPassword(quality);
                    return z;
                }
            }
        } else if (TextUtils.isEmpty(credential) || credential.length() < 4) {
            int i3 = requestedQuality;
            throw new IllegalArgumentException("password must not be null and at least of length 4");
        } else {
            int quality2 = computePasswordQuality(i, str, requestedQuality);
            int quality3 = quality2;
            if (!localService.setLockCredentialWithToken(str, i, tokenHandle, token, quality2, quality)) {
                return false;
            }
            setKeyguardStoredPasswordQuality(quality3, quality);
            updateEncryptionPasswordIfNeeded(str, quality3, quality);
            updatePasswordHistory(str, quality);
            onAfterChangingPassword(quality);
        }
        z = true;
        onAfterChangingPassword(quality);
        return z;
    }

    public boolean unlockUserWithToken(long tokenHandle, byte[] token, int userId) {
        return getLockSettingsInternal().unlockUserWithToken(tokenHandle, token, userId);
    }

    public void enableSyntheticPassword() {
        setLong(SYNTHETIC_PASSWORD_ENABLED_KEY, 1, 0);
    }

    public void disableSyntheticPassword() {
        setLong(SYNTHETIC_PASSWORD_ENABLED_KEY, 0, 0);
    }

    public boolean isSyntheticPasswordEnabled() {
        return getLong(SYNTHETIC_PASSWORD_ENABLED_KEY, 0, 0) != 0;
    }

    public static boolean userOwnsFrpCredential(Context context, UserInfo info) {
        return info != null && info.isPrimary() && info.isAdmin() && frpCredentialEnabled(context);
    }

    public static boolean frpCredentialEnabled(Context context) {
        return context.getResources().getBoolean(17956952);
    }

    public void monitorCheckPassword(int errorType, Exception e) {
        try {
            SharedPreferences sp = this.mContext.getSharedPreferences(SPF_XML_NAME, 0);
            long currentTime = System.currentTimeMillis();
            if (currentTime > ONE_DAY + sp.getLong(KEY_PREFIX + errorType, 0)) {
                Bundle bundle = new Bundle();
                bundle.putInt("errorType", errorType);
                bundle.putSerializable("reason", e);
                mMonitor.monitor(907034001, bundle);
                SharedPreferences.Editor editor = this.mContext.getSharedPreferences(SPF_XML_NAME, 0).edit();
                editor.putLong(KEY_PREFIX + errorType, currentTime).apply();
            } else {
                Log.w(TAG, "monitorCheckPassword: " + errorType + " already report, ignore it!");
            }
        } catch (Exception e2) {
            Log.w(TAG, "monitorCheckPassword: " + errorType + " monitorCheckPassword RequestThrottledException!");
        }
    }
}
