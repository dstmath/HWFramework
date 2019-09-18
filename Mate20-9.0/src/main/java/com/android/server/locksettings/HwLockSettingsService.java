package com.android.server.locksettings;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.security.keystore.KeyProtection;
import android.service.gatekeeper.GateKeeperResponse;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.widget.ICheckCredentialProgressCallback;
import com.android.internal.widget.VerifyCredentialResponse;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.locksettings.LockSettingsService;
import com.android.server.locksettings.LockSettingsStorage;
import com.android.server.locksettings.SyntheticPasswordManager;
import com.android.server.security.antimal.HwAntiMalStatus;
import com.huawei.android.os.UserManagerEx;
import com.huawei.pwdprotect.PwdProtectManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import org.json.JSONException;
import org.json.JSONObject;

public class HwLockSettingsService extends LockSettingsService {
    public static final String DB_KEY_PRIVACY_USER_PWD_PROTECT = "privacy_user_pwd_protect";
    public static final String DESCRIPTOR = "com.android.internal.widget.ILockSettings";
    private static final String EMPTY_APP_NAME = "";
    private static final boolean ENHANCED_GK_RULE = SystemProperties.getBoolean("ro.config.use_passwd_length_rule", false);
    private static final String JSON_ERROR_COUNT = "error";
    private static final String JSON_LEVEL = "level";
    private static final String JSON_START_ELAPSED = "elapsed";
    private static final String JSON_START_RTC = "rtc";
    private static final String JSON_STOP_ELAPSED = "stop";
    private static final String KEY_CREDENTIAL_LEN = "lockscreen.hw_credential_len";
    private static final String KEY_HAS_HW_LOCK_HINT = "lockscreen.has_hw_hint_info";
    private static final String KEY_HW_LOCK_HINT = "lockscreen.hw_hint_info";
    private static final String KEY_HW_PIN_TYPE = "lockscreen.pin_type";
    private static final long KEY_UNLOCK_TYPE_NOT_SET = -1;
    private static final String KEY_UPDATE_WEAK_AUTH_TIME = "lockscreen.hw_weak_auth_time";
    public static final String LOCK_PASSWORD_FILE2 = "password2.key";
    public static final String NO_PWD_FOR_PWD_PROTECT = "no_pwd_for_protect_protect";
    private static final String NRLTAG = "LockSettingsRule";
    private static final int PASSWORD_STATUS_CHANGED = 2;
    private static final int PASSWORD_STATUS_OFF = 0;
    private static final int PASSWORD_STATUS_ON = 1;
    private static final String PERMISSION = "android.permission.ACCESS_KEYGUARD_SECURE_STORAGE";
    private static final String PERMISSION_GET_LOCK_PASSWORD_CHANGED = "com.huawei.permission.GET_LOCK_PASSWORD_CHANGED";
    private static final String PKG_HWOUC = "com.huawei.android.hwouc";
    private static final String PKG_SECURITYMGR = "com.huawei.securitymgr";
    private static final String PKG_SETTINGS = "com.android.settings";
    private static final String PKG_SYSTEMUI = "com.android.systemui";
    private static final String PROFILE_KEY_USER_HINT_DECRYPT = "profile_key_user_hint_decrypt_";
    private static final String PROFILE_KEY_USER_HINT_ENCRYPT = "profile_key_user_hint_encrypt_";
    private static final String PWD_ERROR_COUNT = "gk_rule_error_count";
    private static final String PWD_REGEX = "^\\d{4,32}$";
    private static final String PWD_START_TIME_ELAPSED = "password_start_time_elapsed";
    private static final String PWD_VERIFY_INFO = "password_verification_information";
    private static final String RECEIVER_ACTION_LOCK_PASSWORD_CHANGED = "com.huawei.locksettingsservice.action.LOCK_PASSWORD_CHANGED";
    private static final String RECEIVER_PACKAGE = "com.huawei.hwid";
    private static final int SECURITY_LOCK_SETTINGS = 2;
    private static final String SYSTEM_DIRECTORY = "/system/";
    private static final String TAG = "HwLKSS";
    private static final String USER_LOCK_HINT_FILE = "hw_lock_hint.key";
    private static final int WEAK_AUTH_FACE = 10001;
    private static final int WEAK_AUTH_FINGER = 10002;
    private static ArrayList<Integer> mUseGKRulePids = new ArrayList<>();
    public static final int transaction_checkvisitorpassword = 1002;
    public static final int transaction_setlockvisitorpassword = 1001;
    private final Context mContext;
    private HwAntiMalStatus mHwAntiMalStatus = null;
    private Pattern mPwdPattern;

    static class HwInjector extends LockSettingsService.Injector {
        public HwInjector(Context context) {
            super(context);
        }

        public LockSettingsStrongAuth getStrongAuth() {
            return new HwLockSettingsStrongAuth(this.mContext);
        }

        public SyntheticPasswordManager getSyntheticPasswordManager(LockSettingsStorage storage) {
            return new HwSyntheticPasswordManager(getContext(), storage, getUserManager());
        }

        /* access modifiers changed from: package-private */
        public LockSettingsStorage newStorage() {
            return new HwLockSettingsStorage(this.mContext);
        }
    }

    public HwLockSettingsService(Context context) {
        super(new HwInjector(context));
        this.mContext = context;
        this.mHwAntiMalStatus = new HwAntiMalStatus(this.mContext);
        this.mPwdPattern = Pattern.compile(PWD_REGEX);
    }

    private void setVisitorLockPassword(String password, int userId) throws RemoteException {
        checkWritePermission(userId);
        flog(TAG, "setVisitorLockPassword U" + userId);
        setKeystorePassword(password, userId);
    }

    public boolean checkVisitorPassword(String password, int userId) throws RemoteException {
        Slog.e(TAG, "checkVisitorPassword is deprecated");
        return false;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 1001:
                data.enforceInterface("com.android.internal.widget.ILockSettings");
                setVisitorLockPassword(data.readString(), data.readInt());
                reply.writeInt(0);
                reply.writeNoException();
                return true;
            case 1002:
                data.enforceInterface("com.android.internal.widget.ILockSettings");
                if (checkVisitorPassword(data.readString(), data.readInt())) {
                    reply.writeInt(0);
                } else {
                    reply.writeInt(1);
                }
                reply.writeNoException();
                return true;
            default:
                return HwLockSettingsService.super.onTransact(code, data, reply, flags);
        }
    }

    /* access modifiers changed from: protected */
    public int getOldCredentialType(int userId) {
        LockSettingsStorage.CredentialHash oldCredentialHash = this.mStorage.readCredentialHash(userId);
        if (oldCredentialHash != null) {
            return oldCredentialHash.type;
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public int getPasswordStatus(int currentCredentialType, int oldCredentialType) {
        Slog.i(TAG, "getPasswordStatus, currentCredentialType=" + currentCredentialType + ", oldCredentialType=" + oldCredentialType);
        if (currentCredentialType == -1) {
            return 0;
        }
        if (oldCredentialType == -1) {
            return 1;
        }
        return 2;
    }

    /* access modifiers changed from: protected */
    public void notifyPasswordStatusChanged(int userId, int status) {
        Intent intent = new Intent(RECEIVER_ACTION_LOCK_PASSWORD_CHANGED);
        intent.setPackage(RECEIVER_PACKAGE);
        intent.putExtra("status", status);
        Slog.i(TAG, "notifyPasswordStatusChanged:" + status + ", userId:" + userId);
        this.mContext.sendBroadcastAsUser(intent, new UserHandle(userId), PERMISSION_GET_LOCK_PASSWORD_CHANGED);
        if (status != 0) {
            this.mStrongAuth.reportSuccessfulStrongAuthUnlock(userId);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyModifyPwdForPrivSpacePwdProtect(String credential, String savedCredential, int userId) {
        if (isPrivSpacePwdProtectOpened() && isPrivacyUser(this.mContext, userId)) {
            Slog.i(TAG, "notify privPw ");
            if (!PwdProtectManager.getInstance().modifyPrivPwd(credential)) {
                Log.e(TAG, "modifyPrivSpacePn fail");
            }
            if (UserHandle.myUserId() == 0) {
                Flog.bdReport(this.mContext, 131);
            }
        }
        if (isPrivSpacePwdProtectOpened() && userId == 0) {
            Slog.i(TAG, "notify mainPw ");
            if (TextUtils.isEmpty(savedCredential)) {
                savedCredential = NO_PWD_FOR_PWD_PROTECT;
            }
            if (TextUtils.isEmpty(credential)) {
                credential = NO_PWD_FOR_PWD_PROTECT;
            }
            if (!PwdProtectManager.getInstance().modifyMainPwd(savedCredential, credential)) {
                Log.e(TAG, "modifyMainSpacePn fail");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyBigDataForPwdProtectFail(int userId) {
        if (isPrivSpacePwdProtectOpened() && isPrivacyUser(this.mContext, userId) && UserHandle.myUserId() == 0) {
            Flog.bdReport(this.mContext, 132);
        }
    }

    private boolean isPrivacyUser(Context context, int userId) {
        if (context == null) {
            return false;
        }
        return UserManagerEx.isHwHiddenSpace(UserManagerEx.getUserInfoEx((UserManager) context.getSystemService("user"), userId));
    }

    private boolean isPrivSpacePwdProtectOpened() {
        boolean z = false;
        if (this.mContext == null) {
            return false;
        }
        if (Settings.Global.getInt(this.mContext.getContentResolver(), DB_KEY_PRIVACY_USER_PWD_PROTECT, 0) == 1) {
            z = true;
        }
        return z;
    }

    @VisibleForTesting
    private void saveUserHintMessage(int userId, String hint) throws RuntimeException, IOException {
        KeyStore keyStore;
        byte[] randomLockSeed = hint.getBytes(StandardCharsets.UTF_8);
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            keyStore.setEntry(PROFILE_KEY_USER_HINT_ENCRYPT + userId, new KeyStore.SecretKeyEntry(secretKey), new KeyProtection.Builder(1).setBlockModes(new String[]{"GCM"}).setEncryptionPaddings(new String[]{"NoPadding"}).build());
            keyStore.setEntry(PROFILE_KEY_USER_HINT_DECRYPT + userId, new KeyStore.SecretKeyEntry(secretKey), new KeyProtection.Builder(2).setBlockModes(new String[]{"GCM"}).setEncryptionPaddings(new String[]{"NoPadding"}).setCriticalToDeviceEncryption(true).build());
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(1, (SecretKey) keyStore.getKey(PROFILE_KEY_USER_HINT_ENCRYPT + userId, null));
            byte[] encryptionResult = cipher.doFinal(randomLockSeed);
            byte[] iv = cipher.getIV();
            keyStore.deleteEntry(PROFILE_KEY_USER_HINT_ENCRYPT + userId);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                if (iv.length == 12) {
                    outputStream.write(iv);
                    outputStream.write(encryptionResult);
                    writeHwUserLockHint(userId, outputStream.toByteArray());
                    return;
                }
                throw new RuntimeException("Invalid iv length: " + iv.length);
            } catch (IOException e) {
                throw new RuntimeException("Failed to concatenate byte arrays", e);
            }
        } catch (IOException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e2) {
            throw new RuntimeException("Failed to encrypt key", e2);
        } catch (Throwable th) {
            keyStore.deleteEntry(PROFILE_KEY_USER_HINT_ENCRYPT + userId);
            throw th;
        }
    }

    @VisibleForTesting
    private String getUserHintMessage(int userId) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, CertificateException, IOException {
        byte[] storedData = readHwUserLockHint(userId);
        if (storedData != null) {
            byte[] iv = Arrays.copyOfRange(storedData, 0, 12);
            byte[] encryptedPassword = Arrays.copyOfRange(storedData, 12, storedData.length);
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(2, (SecretKey) keyStore.getKey(PROFILE_KEY_USER_HINT_DECRYPT + userId, null), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encryptedPassword), StandardCharsets.UTF_8);
        }
        throw new FileNotFoundException("Child profile lock file not found");
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public String getHwUserLockHintFile(int userId) {
        return this.mStorage.getLockCredentialFilePathForUser(userId, USER_LOCK_HINT_FILE);
    }

    @VisibleForTesting
    private void writeHwUserLockHint(int userId, byte[] lock) {
        this.mStorage.writeFile(getHwUserLockHintFile(userId), lock);
    }

    private byte[] readHwUserLockHint(int userId) {
        return this.mStorage.readFile(getHwUserLockHintFile(userId));
    }

    private boolean hasHwUserLockHint(int userId) {
        return this.mStorage.hasFile(getHwUserLockHintFile(userId));
    }

    public void setString(String key, String value, int userId) {
        if (KEY_HW_LOCK_HINT.equals(key)) {
            checkWritePermission(userId);
            if ("com.android.settings".equals(getPackageNameFromPid(this.mContext, Binder.getCallingPid()))) {
                try {
                    if (TextUtils.isEmpty(value)) {
                        this.mStorage.deleteFile(getHwUserLockHintFile(userId));
                        Log.v(TAG, "delete UserHintMessage succ : " + key);
                    } else {
                        saveUserHintMessage(userId, value);
                        Log.v(TAG, "save UserHintMessage succ : " + key);
                    }
                    return;
                } catch (IOException | RuntimeException e) {
                    Log.e(TAG, "save fail");
                }
            }
        }
        if (PWD_VERIFY_INFO.equals(key) && isUseGKRule(userId)) {
            resetTime(userId);
        } else if (!PWD_START_TIME_ELAPSED.equals(key) || !isUseGKRule(userId)) {
            HwLockSettingsService.super.setString(key, value, userId);
        } else {
            try {
                JSONObject obj = new JSONObject(value);
                setStartTime(userId, obj.getLong(JSON_START_RTC), obj.getLong(JSON_START_ELAPSED), obj.getLong(JSON_STOP_ELAPSED));
            } catch (JSONException e2) {
                Log.e(TAG, "setStartTime JSONException");
            }
        }
    }

    private boolean isCalledFromSysUI(int pid) {
        return "com.android.systemui".equals(getPackageNameFromPid(this.mContext, pid));
    }

    public long getLong(String key, long defaultValue, int userId) {
        if (!KEY_CREDENTIAL_LEN.equals(key) || isCalledFromSettings(Binder.getCallingPid())) {
            return HwLockSettingsService.super.getLong(key, defaultValue, userId);
        }
        return -1;
    }

    public void setLong(String key, long value, int userId) {
        if (KEY_CREDENTIAL_LEN.equals(key)) {
            Log.i(TAG, "invalid set credential from UID: " + Binder.getCallingUid());
        } else if (KEY_UPDATE_WEAK_AUTH_TIME.equals(key)) {
            if (isCalledFromSysUI(Binder.getCallingPid())) {
                reportSuccessfulWeakAuthUnlock((int) value, userId);
            }
        } else {
            HwLockSettingsService.super.setLong(key, value, userId);
        }
    }

    /* access modifiers changed from: protected */
    public void setLockCredentialInternal(String credential, int credentialType, String savedCredential, int requestedQuality, int userId) throws RemoteException {
        HwLockSettingsService.super.setLockCredentialInternal(credential, credentialType, savedCredential, requestedQuality, userId);
        long len = TextUtils.isEmpty(credential) ? 0 : (long) credential.length();
        flog(TAG, "setLockCredentialInternal U" + userId + " quality:" + requestedQuality);
        HwLockSettingsService.super.setLong(KEY_CREDENTIAL_LEN, len, userId);
    }

    private void reportSuccessfulWeakAuthUnlock(int type, int userId) {
        if ("com.android.systemui".equals(getPackageNameFromPid(this.mContext, Binder.getCallingPid()))) {
            if ((getStrongAuthForUser(userId) & 16) != 0) {
                Slog.e(TAG, "report WeakAuth but already timeout " + type);
            } else if (hasModifiedStrongAuthTime(userId)) {
                Slog.w(TAG, "report WeakAuth return, strongAuthTime is modified");
            } else {
                if (10001 == type || 10002 == type) {
                    this.mStrongAuth.reportSuccessfulWeakAuthUnlock(userId);
                }
            }
        }
    }

    private boolean hasModifiedStrongAuthTime(int userId) {
        boolean z = false;
        if (this.mContext == null) {
            return false;
        }
        if (((DevicePolicyManager) this.mContext.getSystemService("device_policy")).getRequiredStrongAuthTimeout(null, userId) != 259200000) {
            z = true;
        }
        return z;
    }

    public String getString(String key, String defaultValue, int userId) {
        if (KEY_HW_LOCK_HINT.equals(key) && hasHwUserLockHint(userId)) {
            checkWritePermission(userId);
            String callProcessAppName = getPackageNameFromPid(this.mContext, Binder.getCallingPid());
            if ("com.android.systemui".equals(callProcessAppName) || "com.android.settings".equals(callProcessAppName)) {
                try {
                    String hintInfo = getUserHintMessage(userId);
                    return TextUtils.isEmpty(hintInfo) ? defaultValue : hintInfo;
                } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
                    Log.e(TAG, "get UserHintMessage fail");
                }
            }
        }
        if (PWD_VERIFY_INFO.equals(key) && isUseGKRule(userId)) {
            return getVerifyInfo(userId).toString();
        }
        if (!PWD_ERROR_COUNT.equals(key) || !isUseGKRule(userId)) {
            return HwLockSettingsService.super.getString(key, defaultValue, userId);
        }
        return String.valueOf(getErrorCount(userId));
    }

    public boolean getBoolean(String key, boolean defaultValue, int userId) {
        checkWritePermission(userId);
        if (KEY_HAS_HW_LOCK_HINT.equals(key)) {
            return hasHwUserLockHint(userId);
        }
        return HwLockSettingsService.super.getBoolean(key, defaultValue, userId);
    }

    private static String getPackageNameFromPid(Context context, int pid) {
        ActivityManager am = (ActivityManager) context.getSystemService("activity");
        if (am == null) {
            Log.e(TAG, "can't get ACTIVITY_SERVICE");
            return "";
        }
        List<ActivityManager.RunningAppProcessInfo> acts = am.getRunningAppProcesses();
        if (acts == null) {
            Log.e(TAG, "can't get Running App");
            return "";
        }
        int len = acts.size();
        for (int i = 0; i < len; i++) {
            ActivityManager.RunningAppProcessInfo rapInfo = acts.get(i);
            if (rapInfo.pid == pid) {
                if (canUserGKRulePid(rapInfo.processName)) {
                    mUseGKRulePids.add(Integer.valueOf(pid));
                }
                return rapInfo.processName;
            }
        }
        return "";
    }

    private VerifyCredentialResponse verifyCredentialEx(int userId, LockSettingsStorage.CredentialHash storedHash, String credential, boolean hasChallenge, long challenge, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        if ((storedHash == null || storedHash.hash.length == 0) && TextUtils.isEmpty(credential)) {
            Slog.w(TAG, "no stored Password/Pattern, verifyCredential success");
            return VerifyCredentialResponse.OK;
        } else if (storedHash == null || TextUtils.isEmpty(credential)) {
            Slog.w(TAG, "no entered Password/Pattern, verifyCredential ERROR");
            return VerifyCredentialResponse.ERROR;
        } else {
            StrictMode.noteDiskRead();
            try {
                if (getGateKeeperService() == null) {
                    return VerifyCredentialResponse.ERROR;
                }
                return convertResponse(getGateKeeperService().verifyChallenge(userId, challenge, storedHash.hash, credential.getBytes(StandardCharsets.UTF_8)));
            } catch (RemoteException e) {
                return VerifyCredentialResponse.ERROR;
            }
        }
    }

    private VerifyCredentialResponse doVerifyCredentialEx(String credential, int credentialType, boolean hasChallenge, long challenge, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        int i = userId;
        LockSettingsStorage.CredentialHash storedHash = this.mStorage.readCredentialHashEx(i);
        if (storedHash != null && storedHash.hash != null && storedHash.hash.length != 0) {
            return verifyCredentialEx(i, storedHash, credential, hasChallenge, challenge, progressCallback);
        }
        Slog.w(TAG, "no Pattern saved VerifyPattern success");
        return VerifyCredentialResponse.OK;
    }

    private boolean checkPasswordMatch(String password) {
        if (this.mPwdPattern.matcher(password).matches()) {
            return true;
        }
        return false;
    }

    private boolean checkPasswordEx(String password, int userId, ICheckCredentialProgressCallback progressCallback) {
        if (password == null || password.equals("")) {
            return false;
        }
        try {
            if (doVerifyCredentialEx(password, 2, false, 0, userId, progressCallback).getResponseCode() == 0) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public void resetKeyStore(int userId) throws RemoteException {
        HwLockSettingsService.super.resetKeyStore(userId);
        flog(TAG, "resetKeyStore fin U" + userId);
    }

    private byte[] enrollCredentialEx(byte[] enrolledHandle, String enrolledCredential, String toEnroll, int userId) throws RemoteException {
        byte[] toEnrollBytes = null;
        byte[] enrolledCredentialBytes = enrolledCredential == null ? null : enrolledCredential.getBytes(StandardCharsets.UTF_8);
        if (toEnroll != null) {
            toEnrollBytes = toEnroll.getBytes(StandardCharsets.UTF_8);
        }
        if (getGateKeeperService() == null) {
            Slog.e(TAG, "getGateKeeperService fail.");
            return new byte[0];
        }
        GateKeeperResponse response = getGateKeeperService().enroll(userId, enrolledHandle, enrolledCredentialBytes, toEnrollBytes);
        if (response != null) {
            return response.getPayload();
        }
        Slog.w(TAG, "enrollCredential response null");
        return new byte[0];
    }

    private void checkPasswordReadPermission(int userId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, "LockSettingsRead");
    }

    private VerifyCredentialResponse convertResponse(GateKeeperResponse gateKeeperResponse) {
        return VerifyCredentialResponse.fromGateKeeperResponse(gateKeeperResponse);
    }

    public boolean setExtendLockScreenPassword(String password, String phoneNumber, int userHandle) {
        checkWritePermission(userHandle);
        if (!checkPasswordMatch(password)) {
            Slog.i(TAG, "wrong unlock password");
            return false;
        }
        warnLog(TAG, "setExtendLockScreenPassword U" + userHandle + " UID:" + Binder.getCallingUid());
        try {
            byte[] enrolledHandle = enrollCredentialEx(this.mStorage.readCredentialHashEx(userHandle).hash, null, password, userHandle);
            if (enrolledHandle.length == 0) {
                return false;
            }
            this.mStorage.writeCredentialHashEx(LockSettingsStorage.CredentialHash.create(enrolledHandle, 2), userHandle);
            Intent intent = new Intent("com.huawei.intent.action.OPERATOR_REMOTE_LOCK");
            intent.setPackage("com.android.systemui");
            intent.putExtra("PhoneNumber", phoneNumber);
            this.mContext.sendBroadcast(intent);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean clearExtendLockScreenPassword(String password, int userHandle) {
        checkPasswordReadPermission(userHandle);
        warnLog(TAG, "SetLockCredential U" + userHandle + " UID:" + Binder.getCallingUid());
        if (!this.mStorage.hasSetPassword(userHandle)) {
            Slog.i(TAG, "has not set password");
            return false;
        } else if (!checkPasswordMatch(password)) {
            Slog.i(TAG, "cannot match password");
            return false;
        } else if (checkPasswordEx(password, userHandle, null)) {
            this.mStorage.deleteExPasswordFile(userHandle);
            Intent intent = new Intent("com.huawei.intent.action.OPERATOR_REMOTE_UNLOCK");
            intent.setPackage("com.android.systemui");
            this.mContext.sendBroadcast(intent);
            return true;
        } else {
            Slog.e(TAG, "wrong unlock password");
            return false;
        }
    }

    public void handleUserClearLockForAnti(int userId) {
        if (this.mHwAntiMalStatus == null) {
            this.mHwAntiMalStatus = new HwAntiMalStatus(this.mContext);
        }
        this.mHwAntiMalStatus.handleUserClearLockForAntiMal(userId);
    }

    public void setLockCredential(String credential, int type, String savedCredential, int requestedQuality, int userId) throws RemoteException {
        clearPasswordType(userId);
        warnLog(TAG, "SetLockCredential U" + userId + " T" + type + " UID:" + Binder.getCallingUid());
        HwLockSettingsService.super.setLockCredential(credential, type, savedCredential, requestedQuality, userId);
    }

    public boolean setLockCredentialWithToken(String credential, int type, long tokenHandle, byte[] token, int requestedQuality, int userId) throws RemoteException {
        clearPasswordType(userId);
        warnLog(TAG, "setLockCredentialWithToken U" + userId + " T" + type + " UID:" + Binder.getCallingUid());
        return HwLockSettingsService.super.setLockCredentialWithToken(credential, type, tokenHandle, token, requestedQuality, userId);
    }

    private void clearPasswordType(int userId) throws RemoteException {
        HwLockSettingsService.super.setLong(KEY_HW_PIN_TYPE, -1, userId);
    }

    private int getErrorCount(int userId) {
        return Integer.parseInt(this.mStorage.readKeyValue(JSON_ERROR_COUNT, "0", userId));
    }

    private void addErrorCount(int userId) {
        int errorCount = getErrorCount(userId) + 1;
        Slog.w(NRLTAG, "locksettingsservice addErrorCount=" + errorCount + ", userId " + userId);
        this.mStorage.writeKeyValue(JSON_ERROR_COUNT, String.valueOf(errorCount), userId);
    }

    private void resetErrorCount(int userId) {
        Slog.w(NRLTAG, "locksettingsservice resetErrorCount, userId" + userId);
        this.mStorage.writeKeyValue(JSON_ERROR_COUNT, String.valueOf(0), userId);
        resetTime(userId);
    }

    private void setStartTime(int userId, long startTimeRTC, long startTimeElapsed, long stopTimeInFuture) {
        if (startTimeElapsed >= SystemClock.elapsedRealtime() || stopTimeInFuture > SystemClock.elapsedRealtime()) {
            this.mStorage.writeKeyValue(JSON_START_RTC, String.valueOf(startTimeRTC), userId);
            this.mStorage.writeKeyValue(JSON_START_ELAPSED, String.valueOf(startTimeElapsed), userId);
        }
    }

    private void resetTime(int userId) {
        this.mStorage.writeKeyValue(JSON_START_RTC, String.valueOf(0), userId);
        this.mStorage.writeKeyValue(JSON_START_ELAPSED, String.valueOf(0), userId);
    }

    private long getStartTimeRTC(int userId) {
        return Long.parseLong(this.mStorage.readKeyValue(JSON_START_RTC, "0", userId));
    }

    private long getStartTimeElapsed(int userId) {
        return Long.parseLong(this.mStorage.readKeyValue(JSON_START_ELAPSED, "0", userId));
    }

    private synchronized JSONObject getVerifyInfo(int userId) {
        JSONObject obj;
        obj = new JSONObject();
        try {
            obj.put("level", 2);
            obj.put(JSON_ERROR_COUNT, getErrorCount(userId));
            obj.put(JSON_START_RTC, getStartTimeRTC(userId));
            obj.put(JSON_START_ELAPSED, getStartTimeElapsed(userId));
        } catch (JSONException e) {
            Slog.e(TAG, "toJson error", e);
        }
        return obj;
    }

    /* access modifiers changed from: protected */
    public void setKeystorePassword(String password, int userHandle) {
        HwLockSettingsService.super.setKeystorePassword(password, userHandle);
        warnLog(TAG, "setKeystorePassword fin.U" + userHandle);
    }

    private boolean isCalledFromSettings(int pid) {
        if (mUseGKRulePids.contains(Integer.valueOf(pid))) {
            return true;
        }
        return canUserGKRulePid(getPackageNameFromPid(this.mContext, pid));
    }

    private static boolean canUserGKRulePid(String name) {
        return "com.android.settings".equals(name) || PKG_HWOUC.equals(name) || "com.android.systemui".equals(name) || PKG_SECURITYMGR.equals(name);
    }

    public VerifyCredentialResponse checkCredential(String credential, int type, final int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        VerifyCredentialResponse response;
        if (!isUseGKRule(userId)) {
            response = HwLockSettingsService.super.checkCredential(credential, type, userId, progressCallback);
        } else {
            final ICheckCredentialProgressCallback callback = progressCallback;
            VerifyCredentialResponse response2 = HwLockSettingsService.super.checkCredential(credential, type, userId, new ICheckCredentialProgressCallback.Stub() {
                public void onCredentialVerified() throws RemoteException {
                    if (callback != null) {
                        callback.onCredentialVerified();
                    }
                    HwLockSettingsService.this.checkError(0, userId, 0);
                }
            });
            checkError(response2.getResponseCode(), userId, response2.getTimeout());
            response = response2;
        }
        warnLog(TAG, "checkCredential U" + userId + " R" + response.getResponseCode() + " type:" + type);
        return response;
    }

    public VerifyCredentialResponse verifyCredential(String credential, int type, long challenge, int userId) throws RemoteException {
        VerifyCredentialResponse response = HwLockSettingsService.super.verifyCredential(credential, type, challenge, userId);
        if (isUseGKRule(userId)) {
            checkError(response.getResponseCode(), userId, response.getTimeout());
        }
        warnLog(TAG, "checkCredential U" + userId + " R" + response.getResponseCode() + " type:" + type);
        return response;
    }

    /* access modifiers changed from: private */
    public void checkError(int responseCode, int userId, int timeOut) {
        warnLog(TAG, "checkError rCode:" + responseCode + " U" + userId + " " + timeOut);
        switch (responseCode) {
            case -1:
                addErrorCount(userId);
                return;
            case 0:
                resetErrorCount(userId);
                resetStrongAuth(userId);
                return;
            case 1:
                long startTimeElapsed = SystemClock.elapsedRealtime();
                int i = userId;
                setStartTime(i, System.currentTimeMillis(), startTimeElapsed, startTimeElapsed + ((long) timeOut));
                addErrorCount(userId);
                return;
            default:
                return;
        }
    }

    private void resetStrongAuth(int userId) {
        if ((getStrongAuthForUser(userId) & 8) != 0) {
            Slog.w(NRLTAG, "clear AFTER_LOCKOUT_AUTH flag after verifyCredential");
            requireStrongAuth(0, userId);
        }
    }

    private boolean isUseGKRule(int userId) {
        if (!ENHANCED_GK_RULE || !isCalledFromSettings(Binder.getCallingPid())) {
            return false;
        }
        checkWritePermission(userId);
        return true;
    }

    /* access modifiers changed from: protected */
    public SyntheticPasswordManager.AuthenticationToken initializeSyntheticPasswordLocked(byte[] credentialHash, String credential, int credentialType, int requestedQuality, int userId) throws RemoteException {
        if (checkHasBlobFile(userId)) {
            reportCriticalError(HwArbitrationDEFS.MSG_CELL_STATE_DISCONNECT, "update when blob exists");
            flog(TAG, "Initialize SyntheticPassword has unfinish op. U" + userId);
        } else {
            flog(TAG, "Initialize SyntheticPassword U" + userId);
        }
        return HwLockSettingsService.super.initializeSyntheticPasswordLocked(credentialHash, credential, credentialType, requestedQuality, userId);
    }

    private boolean checkHasBlobFile(int userId) {
        File dir = new File("/data/system_de/" + userId + "spblob");
        if (!dir.exists() || !dir.isDirectory()) {
            return false;
        }
        File[] filesList = dir.listFiles();
        if (filesList == null) {
            return false;
        }
        for (File file : filesList) {
            if (file.isFile() && file.getAbsolutePath().endsWith(".secdis")) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void unlockKeystore(String password, int userHandle) {
        HwLockSettingsService.super.unlockKeystore(password, userHandle);
        warnLog(TAG, "unlockKeystore, U" + userHandle);
    }

    /* access modifiers changed from: package-private */
    public void warnLog(String tag, String msg) {
        Slog.w(tag, msg);
        this.mStorage.flog(tag, msg);
    }

    /* access modifiers changed from: package-private */
    public void flog(String tag, String msg) {
        this.mStorage.flog(tag, msg);
    }

    private void reportCriticalError(int errorType, String message) {
        HwFrameworkMonitor mMonitor = HwFrameworkFactory.getHwFrameworkMonitor();
        Bundle bundle = new Bundle();
        bundle.putInt("errorType", errorType);
        bundle.putSerializable("message", message);
        mMonitor.monitor(907034002, bundle);
    }
}
