package com.android.internal.widget;

import android.app.admin.DevicePolicyManager;
import android.app.admin.PasswordMetrics;
import android.app.trust.IStrongAuthTracker.Stub;
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
import android.os.UserManager;
import android.os.storage.IStorageManager;
import android.os.storage.StorageManager;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.LogException;
import android.util.SparseIntArray;
import com.android.internal.R;
import com.android.internal.util.Protocol;
import com.android.internal.widget.LockPatternView.Cell;
import com.google.android.collect.Lists;
import huawei.cust.HwCustUtils;
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
    private static final String IS_TRUST_USUALLY_MANAGED = "lockscreen.istrustusuallymanaged";
    private static final String KEY_PREFIX = "error_type_";
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
    private static HwCustLockPatternUtils mCustLockPatternUtils = ((HwCustLockPatternUtils) HwCustUtils.createObj(HwCustLockPatternUtils.class, new Object[0]));
    private static HwFrameworkMonitor mMonitor = HwFrameworkFactory.getHwFrameworkMonitor();
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private DevicePolicyManager mDevicePolicyManager;
    private final Handler mHandler;
    private ILockSettings mLockSettingsService;
    private UserManager mUserManager;

    public interface CheckCredentialProgressCallback {
        /* renamed from: onEarlyMatched */
        void -com_android_internal_widget_LockPatternUtils$2-mthref-0();
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
        private final int mDefaultStrongAuthFlags;
        private final H mHandler;
        private final SparseIntArray mStrongAuthRequiredForUser;
        protected final Stub mStub;

        private class H extends Handler {
            static final int MSG_ON_STRONG_AUTH_REQUIRED_CHANGED = 1;

            public H(Looper looper) {
                super(looper);
            }

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        StrongAuthTracker.this.handleStrongAuthRequiredChanged(msg.arg1, msg.arg2);
                        return;
                    default:
                        return;
                }
            }
        }

        public StrongAuthTracker(Context context) {
            this(context, Looper.myLooper());
        }

        public StrongAuthTracker(Context context, Looper looper) {
            this.mStrongAuthRequiredForUser = new SparseIntArray();
            this.mStub = new Stub() {
                public void onStrongAuthRequiredChanged(int strongAuthFlags, int userId) {
                    StrongAuthTracker.this.mHandler.obtainMessage(1, strongAuthFlags, userId).sendToTarget();
                }
            };
            this.mHandler = new H(looper);
            this.mDefaultStrongAuthFlags = getDefaultFlags(context);
        }

        public static int getDefaultFlags(Context context) {
            return context.getResources().getBoolean(R.bool.config_strongAuthRequiredOnBoot) ? 1 : 0;
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
                return getLockSettings().getBoolean(IS_TRUST_USUALLY_MANAGED, false, userId);
            } catch (RemoteException e) {
                return false;
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
        Handler handler = null;
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        Looper looper = Looper.myLooper();
        if (looper != null) {
            handler = new Handler(looper);
        }
        this.mHandler = handler;
        if (mCustLockPatternUtils != null && mCustLockPatternUtils.isForbiddenSimplePwdFeatureEnable()) {
            mCustLockPatternUtils.initHwCustLockPatternUtils(getDevicePolicyManager(), this.mContext);
        }
    }

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
        getDevicePolicyManager().reportFailedPasswordAttempt(userId);
        getTrustManager().reportUnlockAttempt(false, userId);
    }

    public void reportSuccessfulPasswordAttempt(int userId) {
        getDevicePolicyManager().reportSuccessfulPasswordAttempt(userId);
        getTrustManager().reportUnlockAttempt(true, userId);
    }

    public void reportPasswordLockout(int timeoutMs, int userId) {
        getTrustManager().reportUnlockLockout(timeoutMs, userId);
    }

    public int getCurrentFailedPasswordAttempts(int userId) {
        return getDevicePolicyManager().getCurrentFailedPasswordAttempts(userId);
    }

    public int getMaximumFailedPasswordsForWipe(int userId) {
        return getDevicePolicyManager().getMaximumFailedPasswordsForWipe(null, userId);
    }

    private byte[] verifyCredential(String credential, int type, long challenge, int userId) throws RequestThrottledException {
        try {
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

    public byte[] verifyPattern(List<Cell> pattern, long challenge, int userId) throws RequestThrottledException {
        throwIfCalledOnMainThread();
        return verifyCredential(patternToString(pattern), 1, challenge, userId);
    }

    public boolean checkPattern(List<Cell> pattern, int userId) throws RequestThrottledException {
        return checkPattern(pattern, userId, null);
    }

    public boolean checkPattern(List<Cell> pattern, int userId, CheckCredentialProgressCallback progressCallback) throws RequestThrottledException {
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
        if (password != null && !password.equals(LogException.NO_VALUE)) {
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

    public boolean checkPasswordHistory(String password, int userId) {
        String passwordHashString = new String(passwordToHash(password, userId), StandardCharsets.UTF_8);
        String passwordHistory = getString(PASSWORD_HISTORY_KEY, userId);
        if (passwordHistory == null) {
            return false;
        }
        int passwordHashLength = passwordHashString.length();
        int passwordHistoryLength = getRequestedPasswordHistoryLength(userId);
        if (passwordHistoryLength == 0) {
            return false;
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

    public void clearLock(String savedCredential, int userHandle) {
        setLong(PASSWORD_TYPE_KEY, 0, userHandle);
        try {
            getLockSettings().setLockCredential(null, -1, savedCredential, userHandle);
        } catch (RemoteException e) {
        }
        Log.i(TAG, "clearLock success by uid = " + Binder.getCallingUid());
        if (userHandle == 0) {
            updateEncryptionPassword(1, null);
            setCredentialRequiredToDecrypt(false);
        }
        onAfterChangingPassword(userHandle);
    }

    public void setLockScreenDisabled(boolean disable, int userId) {
        setBoolean("lockscreen.disabled", disable, userId);
    }

    public boolean isLockScreenDisabled(int userId) {
        boolean z = false;
        if (isSecure(userId)) {
            return false;
        }
        boolean disabledByDefault = this.mContext.getResources().getBoolean(R.bool.config_disableLockscreenByDefault);
        boolean isSystemUser = UserManager.isSplitSystemUser() && userId == 0;
        if (getBoolean("lockscreen.disabled", false, userId)) {
            z = true;
        } else if (disabledByDefault) {
            z = isSystemUser ^ 1;
        }
        return z;
    }

    public void saveLockPattern(List<Cell> pattern, int userId) {
        saveLockPattern(pattern, null, userId);
    }

    public void saveLockPattern(List<Cell> pattern, String savedPattern, int userId) {
        if (pattern != null) {
            try {
                if (pattern.size() >= 4) {
                    setLong(PASSWORD_TYPE_KEY, 65536, userId);
                    getLockSettings().setLockCredential(patternToString(pattern), 1, savedPattern, userId);
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
            String ownerInfo = isOwnerInfoEnabled(userId) ? getOwnerInfo(userId) : LogException.NO_VALUE;
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
        setString("lock_screen_owner_info", info, userId);
        updateCryptoUserInfo(userId);
    }

    public void setOwnerInfoEnabled(boolean enabled, int userId) {
        setBoolean("lock_screen_owner_info_enabled", enabled, userId);
        updateCryptoUserInfo(userId);
    }

    public String getOwnerInfo(int userId) {
        return getString("lock_screen_owner_info", userId);
    }

    public boolean isOwnerInfoEnabled(int userId) {
        return getBoolean("lock_screen_owner_info_enabled", false, userId);
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

    protected void updateEncryptionPassword(final int type, final String password) {
        if (isDeviceEncryptionEnabled()) {
            final IBinder service = ServiceManager.getService("mount");
            if (service == null) {
                Log.e(TAG, "Could not find the mount service to update the encryption password");
            } else {
                new AsyncTask<Void, Void, Void>() {
                    protected Void doInBackground(Void... dummy) {
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

    public void saveLockPassword(String password, String savedPassword, int quality, int userHandle) {
        if (password != null) {
            try {
                if (password.length() >= 4) {
                    writeSettingsData();
                    int computedQuality = PasswordMetrics.computeForPassword(password).quality;
                    setLong(PASSWORD_TYPE_KEY, (long) Math.max(quality, computedQuality), userHandle);
                    getLockSettings().setLockCredential(password, 2, savedPassword, userHandle);
                    updateEncryptionPasswordIfNeeded(password, computedQuality, userHandle);
                    updatePasswordHistory(password, userHandle);
                    if (mCustLockPatternUtils != null && mCustLockPatternUtils.isForbiddenSimplePwdFeatureEnable()) {
                        if (computedQuality != 0) {
                            mCustLockPatternUtils.saveCurrentPwdStatus(mCustLockPatternUtils.currentpwdSimpleCheck(password));
                        } else {
                            mCustLockPatternUtils.saveCurrentPwdStatus(true);
                        }
                    }
                    Log.i(TAG, "saveLockPattern success by uid = " + Binder.getCallingUid());
                    return;
                }
            } catch (RemoteException re) {
                Log.e(TAG, "Unable to save lock password " + re);
            }
        }
        throw new IllegalArgumentException("password must not be null and at least of length 4");
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

    private void updateEncryptionPasswordIfNeeded(String password, int quality, int userHandle) {
        if (userHandle != 0 || !isDeviceEncryptionEnabled()) {
            return;
        }
        if (shouldEncryptWithCredentials(true)) {
            int type;
            boolean numeric = quality == 131072;
            boolean numericComplex = quality == 196608;
            if (numeric || numericComplex) {
                type = 3;
            } else {
                type = 0;
            }
            updateEncryptionPassword(type, password);
            return;
        }
        clearEncryptionPassword();
    }

    private void updatePasswordHistory(String password, int userHandle) {
        String passwordHistory = getString(PASSWORD_HISTORY_KEY, userHandle);
        if (passwordHistory == null) {
            passwordHistory = LogException.NO_VALUE;
        }
        int passwordHistoryLength = getRequestedPasswordHistoryLength(userHandle);
        if (passwordHistoryLength == 0) {
            passwordHistory = LogException.NO_VALUE;
        } else {
            byte[] hash = passwordToHash(password, userHandle);
            passwordHistory = new String(hash, StandardCharsets.UTF_8) + "," + passwordHistory;
            passwordHistory = passwordHistory.substring(0, Math.min(((hash.length * passwordHistoryLength) + passwordHistoryLength) - 1, passwordHistory.length()));
        }
        setString(PASSWORD_HISTORY_KEY, passwordHistory, userHandle);
        onAfterChangingPassword(userHandle);
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
        if (info == null || (info.isManagedProfile() ^ 1) != 0) {
            return false;
        }
        try {
            return getLockSettings().getSeparateProfileChallengeEnabled(userHandle);
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't get separate profile challenge enabled");
            return false;
        }
    }

    public boolean isSeparateProfileChallengeAllowed(int userHandle) {
        UserInfo info = getUserManager().getUserInfo(userHandle);
        if (info == null || (info.isManagedProfile() ^ 1) != 0) {
            return false;
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
            result.add(Cell.of(b2 / 3, b2 % 3));
        }
        return result;
    }

    public static String patternToString(List<Cell> pattern) {
        if (pattern == null) {
            return LogException.NO_VALUE;
        }
        int patternSize = pattern.size();
        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            Cell cell = (Cell) pattern.get(i);
            res[i] = (byte) (((cell.getRow() * 3) + cell.getColumn()) + 49);
        }
        return new String(res);
    }

    public static String patternStringToBaseZero(String pattern) {
        if (pattern == null) {
            return LogException.NO_VALUE;
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
            res[i] = (byte) ((cell.getRow() * 3) + cell.getColumn());
        }
        try {
            return MessageDigest.getInstance(KeyProperties.DIGEST_SHA1).digest(res);
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
            byte[] sha1 = MessageDigest.getInstance(KeyProperties.DIGEST_SHA1).digest(saltedPassword);
            byte[] md5 = MessageDigest.getInstance(KeyProperties.DIGEST_MD5).digest(saltedPassword);
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
        boolean passwordEnabled = (mode == 262144 || mode == 131072 || mode == 196608 || mode == Protocol.BASE_TETHERING || mode == 393216) ? true : mode == 524288;
        return passwordEnabled ? savedPasswordExists(userId) : false;
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
        if (mode == 65536) {
            return savedPatternExists(userId);
        }
        return false;
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
        return System.getIntForUser(this.mContentResolver, System.HAPTIC_FEEDBACK_ENABLED, 1, -2) != 0;
    }

    public long setLockoutAttemptDeadline(int userId, int timeoutMs) {
        long deadline = SystemClock.elapsedRealtime() + ((long) timeoutMs);
        if (mCustLockPatternUtils != null) {
            deadline += mCustLockPatternUtils.needLimit() ? FAILED_ATTEMPT_TIMEOUT_MS - ((long) timeoutMs) : 0;
        }
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
        if (mCustLockPatternUtils == null || (mCustLockPatternUtils.getPowerBtnInstantlyLockDefault() ^ 1) == 0) {
            return getBoolean(LOCKSCREEN_POWER_BUTTON_INSTANTLY_LOCKS, true, userId);
        }
        return getBoolean(LOCKSCREEN_POWER_BUTTON_INSTANTLY_LOCKS, false, userId);
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
        String[] split = serialized.split(",");
        ArrayList<ComponentName> activeTrustAgents = new ArrayList(split.length);
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

    protected void onAfterChangingPassword(int userHandle) {
        getTrustManager().reportEnabledTrustAgentsChanged(userHandle);
    }

    public boolean isCredentialRequiredToDecrypt(boolean defaultValue) {
        int value = Global.getInt(this.mContentResolver, Global.REQUIRE_PASSWORD_TO_DECRYPT, -1);
        if (value == -1) {
            return defaultValue;
        }
        return value != 0;
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
            String str = Global.REQUIRE_PASSWORD_TO_DECRYPT;
            if (!required) {
                i = 0;
            }
            Global.putInt(contentResolver, str, i);
        }
    }

    private boolean isDoNotAskCredentialsOnBootSet() {
        return getDevicePolicyManager().getDoNotAskCredentialsOnBoot();
    }

    protected boolean shouldEncryptWithCredentials(boolean defaultValue) {
        return isCredentialRequiredToDecrypt(defaultValue) ? isDoNotAskCredentialsOnBootSet() ^ 1 : false;
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

    private ICheckCredentialProgressCallback wrapCallback(final CheckCredentialProgressCallback callback) {
        if (callback == null) {
            return null;
        }
        if (this.mHandler != null) {
            return new ICheckCredentialProgressCallback.Stub() {
                public void onCredentialVerified() throws RemoteException {
                    Handler -get0 = LockPatternUtils.this.mHandler;
                    CheckCredentialProgressCallback checkCredentialProgressCallback = callback;
                    checkCredentialProgressCallback.getClass();
                    -get0.post(new -$Lambda$Z_D20fNZ3pzkot0ZIQi5t8fFLYw(checkCredentialProgressCallback));
                }
            };
        }
        throw new IllegalStateException("Must construct LockPatternUtils on a looper thread to use progress callbacks.");
    }

    public long addEscrowToken(byte[] token, int userId) {
        try {
            return getLockSettings().addEscrowToken(token, userId);
        } catch (RemoteException e) {
            return 0;
        }
    }

    public boolean removeEscrowToken(long handle, int userId) {
        try {
            return getLockSettings().removeEscrowToken(handle, userId);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isEscrowTokenActive(long handle, int userId) {
        try {
            return getLockSettings().isEscrowTokenActive(handle, userId);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean setLockCredentialWithToken(String credential, int type, long tokenHandle, byte[] token, int userId) {
        if (type != -1) {
            try {
                if (TextUtils.isEmpty(credential) || credential.length() < 4) {
                    throw new IllegalArgumentException("password must not be null and at least of length 4");
                }
                int computedQuality = PasswordMetrics.computeForPassword(credential).quality;
                if (!getLockSettings().setLockCredentialWithToken(credential, type, tokenHandle, token, userId)) {
                    return false;
                }
                setLong(PASSWORD_TYPE_KEY, (long) Math.max(131072, computedQuality), userId);
                updateEncryptionPasswordIfNeeded(credential, computedQuality, userId);
                updatePasswordHistory(credential, userId);
            } catch (RemoteException re) {
                Log.e(TAG, "Unable to save lock password ", re);
                re.rethrowFromSystemServer();
                return false;
            }
        } else if (!TextUtils.isEmpty(credential)) {
            throw new IllegalArgumentException("password must be emtpy for NONE type");
        } else if (!getLockSettings().setLockCredentialWithToken(null, -1, tokenHandle, token, userId)) {
            return false;
        } else {
            setLong(PASSWORD_TYPE_KEY, 0, userId);
            if (userId == 0) {
                updateEncryptionPassword(1, null);
                setCredentialRequiredToDecrypt(false);
            }
        }
        onAfterChangingPassword(userId);
        return true;
    }

    public void unlockUserWithToken(long tokenHandle, byte[] token, int userId) {
        try {
            getLockSettings().unlockUserWithToken(tokenHandle, token, userId);
        } catch (RemoteException re) {
            Log.e(TAG, "Unable to unlock user with token", re);
            re.rethrowFromSystemServer();
        }
    }

    public void enableSyntheticPassword() {
        setLong(SYNTHETIC_PASSWORD_ENABLED_KEY, 1, 0);
    }

    public boolean isSyntheticPasswordEnabled() {
        return getLong(SYNTHETIC_PASSWORD_ENABLED_KEY, 0, 0) != 0;
    }

    public void monitorCheckPassword(int errorType, Exception e) {
        SharedPreferences sp = this.mContext.getSharedPreferences(SPF_XML_NAME, 0);
        long currentTime = System.currentTimeMillis();
        if (currentTime > 86400000 + sp.getLong(KEY_PREFIX + errorType, 0)) {
            Bundle bundle = new Bundle();
            bundle.putInt("errorType", errorType);
            bundle.putSerializable("reason", e);
            mMonitor.monitor(907034001, bundle);
            this.mContext.getSharedPreferences(SPF_XML_NAME, 0).edit().putLong(KEY_PREFIX + errorType, currentTime).apply();
            return;
        }
        Log.w(TAG, "monitorCheckPassword: " + errorType + " already report, ignore it!");
    }
}
